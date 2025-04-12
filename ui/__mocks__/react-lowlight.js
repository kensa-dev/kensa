const React = require('react');

const Lowlight = React.forwardRef(() => {
    return <div>Mocked Lowlight Component</div>; // Replace this as needed for your test
});

// Mock the registerLanguage function. Jest will replace this on import.
Lowlight.registerLanguage = jest.fn();

module.exports = Lowlight;
