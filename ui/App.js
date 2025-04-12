import React, {useMemo} from 'react';
import './App.scss';
import TestClass from "./test/TestClass";
import Index from "./index/Index";
import {ConfigContext} from "./Util";

const App = () => {
    const {mode, ...config} = useMemo(() => {
        return JSON.parse(document.querySelector("script[id='config']").textContent);
    }, [])

    const renderComponent = () => {
        switch (mode) {
            case 'IndexFile':
                return <Index/>;
            case 'TestFile':
                return <TestClass/>;
        }
    }

    return (
        <ConfigContext.Provider value={config}>
            {renderComponent()}
        </ConfigContext.Provider>
    );
};

export default App

