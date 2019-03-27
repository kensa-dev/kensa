import React from 'react';
import ReactDOM from 'react-dom';
import './index.scss';
import App from './App';
import Lowlight from 'react-lowlight';
import xml from 'highlight.js/lib/languages/xml';
import json from 'highlight.js/lib/languages/json';
import plainText from 'highlight.js/lib/languages/plaintext';

Lowlight.registerLanguage('json', json);
Lowlight.registerLanguage('xml', xml);
Lowlight.registerLanguage('plaintext', plainText);

ReactDOM.render(<App />, document.getElementById('root'));