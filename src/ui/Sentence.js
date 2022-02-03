import React, {Component} from "react";
import "./sentence.scss"

export class Token extends Component {

    constructor(props) {
        super(props);

        this.state = {
            expanded: this.props.expanded || false,
        };

        this.toggle = this.toggle.bind(this);
    }

    acronymExpansion() {
        if (this.props.types.includes("token-acronym")) {
            return this.props.acronyms[this.props.value];
        }
    }

    showWhenExpanded() {
        return this.state.expanded ? "" : "is-hidden"
    }

    classes() {
        const types = this.props.types
        let c = ""

        types.forEach(type => {
            if (c.length > 0) c += " "
            c += type;
        })

        if (types.includes("token-acronym")) {
            c = "tooltip " + c;
        }

        if (types.includes("token-expandable") && this.state.expanded) {
            c += " expanded"
        }

        return c;
    }

    toggle() {
        this.setState(prevState => ({
            expanded: !prevState.expanded
        }))
    }

    render() {
        let types = this.props.types;
        let value = this.props.value;

        if (types.includes("token-expandable")) {
            return <>
                <span onClick={this.toggle}
                      className={this.classes()}
                      data-tooltip={this.acronymExpansion()}>
                    {value}
                </span>
                <span className={this.showWhenExpanded()}>
                   {
                       this.props.tokens.map((tokens) => {
                           return <Sentence nested={true} sentence={tokens} acronyms={this.props.acronyms}/>
                       })
                   }
                </span>
            </>
        } else {
            return <span
                className={this.classes()}
                data-tooltip={this.acronymExpansion()}>
            {value}
                </span>
        }
    }
}

export class Sentence extends Component {

    constructor(props) {
        super(props);

        this.state = {
            nested: this.props.nested || false
        };
    }

    tokensOf(sentence) {
        return sentence.map((token, index) => {
            return <Token key={index} types={token.types} value={token.value} tokens={token.tokens} expanded={false} acronyms={this.props.acronyms}/>
        }).reduce((prev, curr) => [prev, " ", curr])
    }

    isNested() {
        if (this.props.nested) {
            return "nested-sentence"
        }
    }

    render() {
        const sentence = this.props.sentence;

        return (
            <div className={this.isNested()}>{this.tokensOf(sentence)}</div>
        )
    }
}