import React, {useEffect, useState} from "react";
import {stateClassFor} from "../Util";
import Test from "./Test";
import {Information} from "./Information";


const TestClass = ({sectionOrder, issueTrackerUrl}) => {
    const [data, setData] = useState(null);
    const [startExpanded, setStartExpanded] = useState(0);

    useEffect(() => {
        let data = JSON.parse(document.querySelector("script[id^='test-result']").textContent);

        if (window.location.hash) {
            setStartExpanded(data.tests.findIndex(test => window.location.hash.substring(1) === test.testMethod))
        } else {
            setStartExpanded(0)
        }

        setData(data)
    }, [])

    if (data) {
        return (
            <>
                <section className={"hero " + stateClassFor(data.state)}>
                    <div className="hero-body">
                        <h1 className="title">{data.displayName}</h1>
                    </div>
                </section>
                <section className="section">
                    <Information issueTrackerUrl={issueTrackerUrl} issues={data.issues} notes={data.notes}/>
                    {
                        data.tests.map((test, index) =>
                            <Test issueTrackerUrl={issueTrackerUrl} sectionOrder={sectionOrder} test={test} key={index} startExpanded={index === startExpanded}/>
                        )
                    }

                </section>
            </>
        )
    }
}

export default TestClass