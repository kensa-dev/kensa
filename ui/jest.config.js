
module.exports = {
    transform: {
        '^.+\\.(js|jsx|ts|tsx)$': 'babel-jest', // Transpile JS/TS files
    },
    moduleNameMapper: {
        '^react-lowlight$': '<rootDir>/ui/__mocks__/react-lowlight.js',
        '\\.(css|scss)$': '<rootDir>/__mocks__/styleMock.js',
        '\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2)$': '<rootDir>/__mocks__/fileMock.js',
        '^index.js$': '<rootDir>/__mocks__/index.js'

    },
    testEnvironment: 'jsdom'
};
