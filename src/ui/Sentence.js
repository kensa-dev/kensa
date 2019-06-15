import React, {Component} from "react";

export class Sentence extends Component {

    constructor(props) {
        super(props);

        this.state = {
            acronyms: this.props.acronyms
        };
    }

    acronymExpansionFor(type, value) {
        if (type.endsWith("Acronym")) {
            return this.state.acronyms[value];
        }
    }

    classFor(type) {
        let c = "token-" + type.toLowerCase();

        if (type.endsWith("Acronym")) {
            c = "tooltip " + c;
        }

        return c;
    }

    render() {
        const sentence = this.props.sentence;
        return (
                <div>
                    {
                        sentence.map((token, index) => {
                            let type = Object.keys(token)[0];
                            let value = token[type];
                            return <span key={index} className={this.classFor(type)}
                                         data-tooltip={this.acronymExpansionFor(type, value)}>{value}</span>
                        })
                                .reduce((prev, curr) => [prev, " ", curr])
                    }
                </div>
        );
    }
}