import React, {Component} from "react";
import "./sentence.scss"

export class Sentence extends Component {

    constructor(props) {
        super(props);

        this.state = {
            expanded: this.props.expanded || false,
            nested: this.props.nested || false
        };

        this.toggle = this.toggle.bind(this);
    }

    acronymExpansionFor(types, value) {
        if (types.includes("Acronym")) {
            return this.props.acronyms[value];
        }
    }

    classesFor(types) {
        let c = ""

        types.forEach(type => {
            if (c.length > 0) c += " "
            c += "token-" + type.toLowerCase();
        })

        if (types.includes("Acronym")) {
            c = "tooltip " + c;
        }

        if (types.includes("Expandable") && this.state.expanded) {
            c += " expanded"
        }

        return c;
    }

    showWhenExpanded() {
        return this.state.expanded ? "" : "is-hidden"
    }

    toggle() {
        this.setState(prevState => ({
            expanded: !prevState.expanded
        }))
    }

    sentence(sentence) {
        return sentence.map((token, index) => {
            let types = token.types;
            let value = token.value;

            if (types.includes("Expandable")) {
                return <>
                        <span key={index}
                              onClick={this.toggle}
                              className={this.classesFor(types)}
                              data-tooltip={this.acronymExpansionFor(types, value)}>
                    {value}
                </span>
                    <span className={this.showWhenExpanded()}>
                        {
                            token.tokens.map((tokens) => {
                                return <Sentence nested={true} sentence={tokens} acronyms={this.props.acronyms}/>
                            })
                        }
                    </span>
                </>
            } else {
                return <span key={index}
                             className={this.classesFor(types)}
                             data-tooltip={this.acronymExpansionFor(types, value)}>
                    {value}
                </span>

            }
        })
                .reduce((prev, curr) => [prev, " ", curr])
    }

    foo() {
        if (this.props.nested) {
            return "nested-sentence"
        }
    }

    render() {
        const sentence = this.props.sentence;

        return (
                <div className={this.foo()}>{this.sentence(sentence)}</div>
        )
    }
}