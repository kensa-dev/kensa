import React from 'react';
import {createRoot} from 'react-dom/client';
import './index.scss';
import App from './App';
import Lowlight from 'react-lowlight';
import xml from 'highlight.js/lib/languages/xml';
import json from 'highlight.js/lib/languages/json';
import plainText from 'highlight.js/lib/languages/plaintext';

Lowlight.registerLanguage('json', json);
Lowlight.registerLanguage('xml', xml);
Lowlight.registerLanguage('plaintext', plainText);

createRoot(document.getElementById('root')).render(<App />);