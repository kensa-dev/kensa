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

    acronymExpansionFor(type, value) {
        if (type.endsWith("Acronym")) {
            return this.props.acronyms[value];
        }
    }

    classFor(type) {
        let c = "token-" + type.toLowerCase();

        if (type.endsWith("Acronym")) {
            c = "tooltip " + c;
        }

        if (type === "Expandable" && this.state.expanded) {
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
            let type = token.type;
            let value = token.value;

            if (token.type === "Expandable") {
                return <>
                        <span key={index}
                              onClick={this.toggle}
                              className={this.classFor(type)}
                              data-tooltip={this.acronymExpansionFor(type, value)}>
                    {value}
                </span>
                    <span className={this.showWhenExpanded()}>
                        <Sentence nested={true} sentence={token.tokens} acronyms={this.props.acronyms}/>
                    </span>
                </>
            } else {
                return <span key={index}
                             className={this.classFor(type)}
                             data-tooltip={this.acronymExpansionFor(type, value)}>
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