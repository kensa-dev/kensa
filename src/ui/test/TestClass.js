import React, {useContext, useEffect, useState} from "react";
import {ConfigContext, stateClassFor} from "../Util";
import Test from "./Test";
import Information from "./information/Information";
import Header from "../Header";
import PackageName from "./PackageName";

const TestClass = () => {
    const {packageDisplayMode} = useContext(ConfigContext)
    const [data, setData] = useState(null);
    const [startExpanded, setStartExpanded] = useState(0);

    useEffect(() => {
        let data = JSON.parse(document.querySelector("script[id^='test-result']").textContent);

        if (window.location.hash) {
            setStartExpanded(data.tests.findIndex(test => window.location.hash.substring(1) === test.testMethod))
        } else {
            setStartExpanded(data.tests.findIndex(test => test.state === "Failed"))
        }

        setData(data)
    }, [])
    
    const packageNameFromClass = (fqClassName) => fqClassName.replace(/\.[^.]+$/, "")
     
    if (data) {
        return (
            <>
                <Header headerClass={stateClassFor(data.state)}>
                    <PackageName packageDisplayMode={packageDisplayMode} fullPackageName={packageNameFromClass(data.testClass)} minimumUniquePackageName={data.minimumUniquePackageName}/>
                    {data.displayName}                        
                </Header>
                <section className="section">
                    <Information issues={data.issues} notes={data.notes}/>
                    {
                        data.tests.map((test, idx) =>
                            <Test test={test} key={idx} startExpanded={idx === startExpanded}/>
                        )
                    }

                </section>
            </>
        )
    }
}

export default TestClass