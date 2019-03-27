import React, {Component} from 'react';
import './App.scss';
import TestWrapper from "./Test";
import MultiPageIndex from "./MultiPageIndex";

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
            isLoaded: false
        };
    }

    static testStateFor(state) {
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
        if (indexNode) {
            indexJson = JSON.parse(indexNode.textContent);
            mode = App.selectModeFrom(indexJson);
        }

        if (mode === Mode.TestFile) {
            let jsonNode = document.querySelector("script[id^='test-result']");
            let jsonData = JSON.parse(jsonNode.textContent);

            this.setState({
                data: jsonData,
                isLoaded: true,
                mode: mode
            })
        } else if (mode === Mode.SingleFile) {
            this.setState({
                indices: indexJson.indices,
                isLoaded: true,
                mode: mode
            });
        } else if (mode === Mode.MultiFile) {
            this.setState({
                indices: indexJson.indices,
                isLoaded: true,
                mode: mode
            });
        }
    }

    renderSingleFile() {

    }

    renderMultiFile() {
        return (
                <div>
                    <section className="hero is-primary">
                        <div className="hero-body">
                            <h1 className="title">Index</h1>
                        </div>
                    </section>
                    <section className="section container">
                        <MultiPageIndex indices={this.state.indices}/>
                    </section>
                </div>
        )
    }

    renderTestFile() {
        const data = this.state.data;
        return (
                <div>
                    <section className={"hero " + App.testStateFor(data.state)}>
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