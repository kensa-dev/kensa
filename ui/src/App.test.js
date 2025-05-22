import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import App from './App';
import {ConfigContext} from "./Util";
import Index from './index/Index';
import TestClass from './test/TestClass';

jest.mock('./index/Index');
jest.mock('./test/TestClass');

describe('App Component', () => {
    beforeEach(() => {
        const root = document.createElement('div');
        root.setAttribute('id', 'root');
        document.body.appendChild(root);
    });

    afterEach(() => {
        document.body.innerHTML = '';
    });

    const setupConfigScript = (config) => {
        const script = document.createElement('script');
        script.id = 'config';
        script.type = 'application/json';
        script.textContent = JSON.stringify(config);
        document.body.appendChild(script);
    };

    it("renders the 'Index' component when mode is 'IndexFile'", () => {
        setupConfigScript({
            mode: 'IndexFile',
            someConfigKey: 'someValue',
        });
        Index.mockImplementation(() => <div>Mocked Index Component</div>);

        render(<App />);

        expect(screen.getByText('Mocked Index Component')).toBeInTheDocument();
    });

    it("renders the 'TestClass' component when mode is 'TestFile'", () => {
        setupConfigScript({
            mode: 'TestFile',
            anotherConfigKey: 'anotherValue',
        });
        TestClass.mockImplementation(() => <div>Mocked TestClass Component</div>);

        render(<App />);

        expect(screen.getByText('Mocked TestClass Component')).toBeInTheDocument();
    });

    it('provides configuration values via ConfigContext', () => {
        const config = {
            mode: 'IndexFile',
            exampleKey: 'exampleValue',
            anotherKey: 'anotherValue',
        };
        setupConfigScript(config);

        let receivedConfig;
        Index.mockImplementation(() => {
            // Access the config from the context during rendering
            receivedConfig = React.useContext(ConfigContext);
            return <div>Mocked Index Component</div>;
        });

        // Act: Render the App component
        render(<App />);

        // Assert: Ensure the context receives the correct values
        expect(receivedConfig).toEqual({ exampleKey: 'exampleValue', anotherKey: 'anotherValue' });
    });
});
