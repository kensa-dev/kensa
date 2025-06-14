import React from 'react';
import {createRoot} from 'react-dom/client';
import App from './App';
import Lowlight from 'react-lowlight';
import xml from 'highlight.js/lib/languages/xml';
import json from 'highlight.js/lib/languages/json';
import plainText from 'highlight.js/lib/languages/plaintext';
import 'highlight.js/styles/intellij-light.min.css'
import {createBrowserRouter, createRoutesFromElements, Route, RouterProvider} from "react-router-dom";

Lowlight.registerLanguage('json', json);
Lowlight.registerLanguage('xml', xml);
Lowlight.registerLanguage('plaintext', plainText);

const router = createBrowserRouter(
    createRoutesFromElements(
        <Route path="/*" element={<App/>}/>
    )
)

createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <RouterProvider router={router}/>
    </React.StrictMode>
)
