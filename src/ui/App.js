import React, {Component} from 'react';
import './App.scss';
import TestWrapper from "./Test";
import Indices from "./IndexPage";

const Mode = {
    SingleFile: 'SingleFile',
    MultiFile: 'MultiFile',
    TestFile: 'TestFile',
    Site: 'Site'
};

export default class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            data: null,
            indices: null,
            isLoaded: false
        };
    }

    static stateClassFor(state) {
        return "test-" + state.toLowerCase();
    }

    static selectModeFrom(json) {
        if (json.mode === Mode.SingleFile) {
            return Mode.SingleFile;
        } else {
            return Mode.MultiFile;
        }
    }

    componentDidMount() {
        let mode = Mode.TestFile;

        let indexNode = document.querySelector("script[id='indices']");
        let indexJson;
        let indices = null;
        let data = null;
        if (indexNode) {
            indexJson = JSON.parse(indexNode.textContent);
            mode = App.selectModeFrom(indexJson);
        }

        switch (mode) {
            case Mode.TestFile:
                data = JSON.parse(document.querySelector("script[id^='test-result']").textContent);
                break;
            case Mode.SingleFile:
            case Mode.MultiFile:
            case Mode.Site:
                indices = indexJson.indices;
        }

        this.setState({
            mode: mode,
            data: data,
            indices: indices,
            isLoaded: true
        });
    }

    renderSingleFile() {

    }

    renderMultiFile() {
        return (
                <div>
                    <section className="hero test-passed">
                        <div className="hero-body">
                            <h1 className="title">Index</h1>
                        </div>
                    </section>
                    <section className="section container">
                        <Indices indices={this.state.indices}/>
                    </section>
                </div>
        )
    }

    renderTestFile() {
        const data = this.state.data;
        return (
                <div>
                    <section className={"hero " + App.stateClassFor(data.state)}>
                        <div className="hero-body">
                            <h1 className="title">{data.displayName}</h1>
                            <h2 className="subtitle">{data.notes}</h2>
                        </div>
                        {/*<div className="hero-foot">*/}
                        {/*<div className="container">*/}
                        {/*<nav className="navbar">*/}
                        {/*<div className="navbar-menu">*/}
                        {/*<div className="navbar-end">*/}
                        {/*<div className="navbar-item is-size-4 has-text-weight-bold">Index</div>*/}
                        {/*<div className="navbar-item has-dropdown is-size-4 has-text-weight-bold">*/}
                        {/*<a className="navbar-link">*/}
                        {/*Docs*/}
                        {/*</a>*/}
                        {/*</div>*/}
                        {/*<div className="navbar-item">*/}
                        {/*<input className="input" type="text" placeholder="Search"/>*/}
                        {/*</div>*/}
                        {/*</div>*/}
                        {/*</div>*/}
                        {/*</nav>*/}
                        {/*</div>*/}
                        {/*</div>*/}
                    </section>
                    <section className="section">
                        <TestWrapper tests={data.tests}/>
                    </section>
                </div>
        );
    }

    render() {
        if (this.state.isLoaded) {
            switch (this.state.mode) {
                case Mode.SingleFile:
                    return this.renderSingleFile();
                case Mode.MultiFile:
                    return this.renderMultiFile();
                case Mode.TestFile:
                    return this.renderTestFile();
            }
        } else {
            return (<div>Loading...</div>)
        }
    }
}