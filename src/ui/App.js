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
            isLoaded: false
        };
    }

    static stateClassFor(state) {
        return "test-" + state.toLowerCase();
    }

    static darkerStateClassFor(state) {
        return "test-" + state.toLowerCase() + "-darker";
    }

    static selectModeFrom(json) {
        return Mode[json.mode];
    }

    componentDidMount() {
        let configNode = document.querySelector("script[id='config']");
        let configJson = JSON.parse(configNode.textContent);
        let mode = App.selectModeFrom(configJson);

        let indices = null;
        let data = null;

        switch (mode) {
            case Mode.TestFile:
                data = JSON.parse(document.querySelector("script[id^='test-result']").textContent);
                break;
            case Mode.SingleFile:
            case Mode.MultiFile:
            case Mode.Site:
                let indexNode = document.querySelector("script[id='indices']");
                let indexJson = JSON.parse(indexNode.textContent);
                indices = indexJson.indices;
        }

        let issueTrackerUrl = configJson["issueTrackerUrl"];
        this.setState({
            issueTrackerUrl: issueTrackerUrl == null ? "#" : issueTrackerUrl,
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
                        <Indices issueTrackerUrl={this.state.issueTrackerUrl} indices={this.state.indices}/>
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
                        <TestWrapper issueTrackerUrl={this.state.issueTrackerUrl} tests={data.tests}/>
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