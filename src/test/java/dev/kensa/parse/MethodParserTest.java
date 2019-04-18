package dev.kensa.parse;

import dev.kensa.render.Renderers;
import dev.kensa.sentence.Dictionary;
import dev.kensa.sentence.Sentence;
import dev.kensa.sentence.Sentences;
import dev.kensa.util.NameValuePair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.github.javaparser.JavaParser.parse;
import static dev.kensa.sentence.SentenceTokens.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class MethodParserTest {

    @AfterEach
    void tearDown() {
        Dictionary.clearAcronyms();
    }

    @Test
    void canParseMethodWithAcronymsInStatement() {
        Dictionary.putAcronym("KCI");

        String code = "class T {\n" +
                "   void testMethod() {\n" +
                "      given(aKCIOf(\"On\"));\n" +
                "   }\n" +
                "}\n";


        List<Sentence> sentences = parseToSentences(code);

        assertThat(sentences).hasSize(1);
        assertThat(sentences.get(0).stream()).containsExactly(
                aKeywordOf("Given"),
                aWordOf("a"),
                anAcronymOf("KCI"),
                aWordOf("of"),
                aStringLiteralOf("On")
        );
    }

    @Test
    void canParseSimpleSingleLineStatementsWithStringLiterals() {
        String code = "class T {\n" +
                "   void testMethod() {\n" +
                "      given(aLightIs(\"On\"));\n" +
                "      when(theLightIsSwitchedOff());\n" +
                "      then(theLightIs(\"Off\"));\n" +
                "   }\n" +
                "}\n";


        List<Sentence> sentences = parseToSentences(code);

        assertThat(sentences).hasSize(3);
        assertThat(sentences.get(0).stream()).containsExactly(
                aKeywordOf("Given"),
                aWordOf("a"),
                aWordOf("light"),
                aWordOf("is"),
                aStringLiteralOf("On")
        );
        assertThat(sentences.get(1).stream()).containsExactly(
                aKeywordOf("When"),
                aWordOf("the"),
                aWordOf("light"),
                aWordOf("is"),
                aWordOf("switched"),
                aWordOf("off")
        );
        assertThat(sentences.get(2).stream()).containsExactly(
                aKeywordOf("Then"),
                aWordOf("the"),
                aWordOf("light"),
                aWordOf("is"),
                aStringLiteralOf("Off")
        );
    }

    @Test
    void canParseAMultilineStatementAndPreserveLineBreaks() {
        String code = "class T {\n" +
                "   void testMethod() {\n" +
                "      then(theLightIs(\"Off\"))\n" +
                "         .and(itIsDark())\n" +
                "         .and(monstersComeOut());\n" +
                "   }\n" +
                "}\n";

        List<Sentence> sentences = parseToSentences(code);

        assertThat(sentences).hasSize(1);
        assertThat(sentences.get(0).stream()).containsExactly(
                aKeywordOf("Then"),
                aWordOf("the"),
                aWordOf("light"),
                aWordOf("is"),
                aStringLiteralOf("Off"),
                aNewline(),
                aKeywordOf("and"),
                aWordOf("it"),
                aWordOf("is"),
                aWordOf("dark"),
                aNewline(),
                aKeywordOf("and"),
                aWordOf("monsters"),
                aWordOf("come"),
                aWordOf("out")
        );
    }

    @Test
    void canParseStatementAndResolveIdentifierValues() {
        String code = "class T {\n" +
                "   private String f1;" +
                "   private Integer f2;" +
                "   void testMethod(String p1, String p2, Integer p3) {\n" +
                "      given(theFirstFieldHasAValueOf(f1));" +
                "      given(theSecondFieldHasAValueOf(f2));" +
                "      then(theFirstParameterIs(p1))\n" +
                "         .and(theSecondParameterIs(p2))\n" +
                "         .and(theThirdParameterIs(p3));\n" +
                "   }\n" +
                "}\n";

        String fieldValue = "fieldValue";
        Integer renderedField = 555;
        String parameterValue1 = "parameterValue1";
        String parameterValue2 = "parameterValue2";
        Integer renderedParameter = 666;

        Map<String, NameValuePair> identifiers = Map.of(
                "f1", new NameValuePair("f1", fieldValue),
                "f2", new NameValuePair("f2", renderedField),
                "p1", new NameValuePair("p1", parameterValue1),
                "p2", new NameValuePair("p2", parameterValue2),
                "p3", new NameValuePair("p3", renderedParameter)
        );

        List<Sentence> sentences = parseToSentences(code, s -> Optional.ofNullable(identifiers.get(s)));

        assertThat(sentences).hasSize(3);
        assertThat(sentences.get(0).stream()).containsExactly(
                aKeywordOf("Given"),
                aWordOf("the"),
                aWordOf("first"),
                aWordOf("field"),
                aWordOf("has"),
                aWordOf("a"),
                aWordOf("value"),
                aWordOf("of"),
                anIdentifierOf(fieldValue)
        );
        assertThat(sentences.get(1).stream()).containsExactly(
                aKeywordOf("Given"),
                aWordOf("the"),
                aWordOf("second"),
                aWordOf("field"),
                aWordOf("has"),
                aWordOf("a"),
                aWordOf("value"),
                aWordOf("of"),
                anIdentifierOf("<<555>>")
        );
        assertThat(sentences.get(2).stream()).containsExactly(
                aKeywordOf("Then"),
                aWordOf("the"),
                aWordOf("first"),
                aWordOf("parameter"),
                aWordOf("is"),
                anIdentifierOf(parameterValue1),
                aNewline(),
                aKeywordOf("and"),
                aWordOf("the"),
                aWordOf("second"),
                aWordOf("parameter"),
                aWordOf("is"),
                anIdentifierOf(parameterValue2),
                aNewline(),
                aKeywordOf("and"),
                aWordOf("the"),
                aWordOf("third"),
                aWordOf("parameter"),
                aWordOf("is"),
                anIdentifierOf("<<666>>")
        );
    }

    private List<Sentence> parseToSentences(String code) {
        return parseToSentences(code, s -> Optional.empty());
    }

    private List<Sentence> parseToSentences(String code, Function<String, Optional<NameValuePair>> identifierProvider) {
        Renderers renderers = new Renderers();
        renderers.add(Integer.class, value -> String.format("<<%d>>", value));

        return parse(code).getClassByName("T")
                          .map(cd -> cd.getMethodsByName("testMethod").get(0))
                          .map(md -> new MethodParser(md, renderers, identifierProvider))
                          .map(MethodParser::sentences)
                          .map(Sentences::stream)
                          .map(s -> s.collect(toList()))
                          .orElseThrow(() -> new RuntimeException("Unable to parse given code"));
    }
}