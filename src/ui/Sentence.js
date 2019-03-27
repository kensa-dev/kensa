import React, {Component} from "react";

export class Sentence extends Component {
    render() {
        const sentence = this.props.sentence;
        return (
                <div>
                    {sentence.map((token, index) => <span key={index} className={"token-" + token.type.toLowerCase()}>{token.value}</span>)
                            .reduce((prev, curr) => [prev, ' ', curr])}
                </div>
        );
    }
}