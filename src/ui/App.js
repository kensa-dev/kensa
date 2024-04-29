import React, {useMemo} from 'react';
import './App.scss';
import TestClass from "./test/TestClass";
import Index from "./index/Index";
import {Section, SectionOrderContext, TrackingUrlContext} from "./Util";

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
            json["issueTrackerUrl"] || "#"
        ]
    }, [])

    switch (mode) {
        case Mode.IndexFile:
            return <Index/>
        case Mode.TestFile:
            return <TrackingUrlContext.Provider value={issueTrackerUrl}>
                <SectionOrderContext.Provider value={sectionOrder}>
                    <TestClass/>
                </SectionOrderContext.Provider>
            </TrackingUrlContext.Provider>
    }
};

export default App

