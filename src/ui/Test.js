import React, {Component} from "react";
import {Invocation} from "./Invocation";
import App from "./App";

class Test extends Component {
    render() {
        const test = this.props.test;
        const disabled = this.props.test.state === 'Disabled';
        if (disabled) {
            return (
                    <div className={"message " + App.stateClassFor(test.state)}>
                        <div className="message-header">{test.displayName}</div>
                        <div className="message-body">
                            Test was not executed.
                        </div>
                    </div>
            );
        } else {
            return (
                    <div className={"message " + App.stateClassFor(test.state)}>
                        <div className="message-header"><p>{test.displayName}</p><div className="is-size-7">{test.notes}</div></div>
                        <div className="message-body">
                            {test.invocations.map((invocation, index) => <Invocation key={index} testMethod={test.testMethod} invocation={invocation} invocationNumber={index}/>)}
                        </div>
                    </div>
            );
        }
    }
}

export default class TestWrapper extends Component {
    render() {
        return (
                this.props.tests.map((test, index) =>
                        <Test key={index} test={test}/>
                )
        );
    }
}