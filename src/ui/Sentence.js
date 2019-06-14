import React, {Component} from "react";

export class Sentence extends Component {

    constructor(props) {
        super(props);

        this.state = {
            acronyms: this.props.acronyms
        };
    }

    acronymExpansionFor(token) {
        if (token.type === "Acronym") {
            return this.state.acronyms[token.value];
        }
    }

    classFor(token) {
        let c = "token-" + token.type.toLowerCase();

        if (token.type === "Acronym") {
            c = "tooltip " + c;
        }

        return c;
    }

    render() {
        const sentence = this.props.sentence;
        return (
            <div>
                {sentence.map((token, index) => <span key={index} className={this.classFor(token)}
                                                      data-tooltip={this.acronymExpansionFor(token)}>{token.value}</span>)
                    .reduce((prev, curr) => [prev, " ", curr])}
            </div>
        );
    }
}