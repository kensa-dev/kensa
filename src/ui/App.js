import React, {useMemo} from 'react';
import './App.scss';
import TestClass from "./test/TestClass";
import Index from "./index/Index";
import {Section} from "./Util";

const App = () => {
    const Mode = {
        IndexFile: 'IndexFile',
        TestFile: 'TestFile'
    };

    const [sectionOrder, mode, issueTrackerUrl] = useMemo(() => {
        const json = JSON.parse(document.querySelector("script[id='config']").textContent);
        return [
            json.sectionOrder.map((value) => Section[value]),
            Mode[json.mode],
            json["issueTrackerUrl"] == null ? "#" : json["issueTrackerUrl"]
        ]
    }, [])

    switch (mode) {
        case Mode.IndexFile:
            return <Index/>
        case Mode.TestFile:
            return <TestClass sectionOrder={sectionOrder} issueTrackerUrl={issueTrackerUrl}/>
    }
};

export default App

