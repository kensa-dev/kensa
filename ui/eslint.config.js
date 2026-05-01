import js from "@eslint/js";
import ts from "typescript-eslint";

export default ts.config(
    js.configs.recommended,
    ...ts.configs.recommended,
    {
        languageOptions: {
            parserOptions: {
                project: "./tsconfig.json",
                ecmaVersion: "latest",
                sourceType: "module",
            },
        },
        rules: {
            "@typescript-eslint/no-explicit-any": "error",
            "no-ternary": "off",
            "@typescript-eslint/no-unused-expressions": ["error", { "allowTernary": true }],
            "no-nested-ternary": "off",
            "no-unneeded-ternary": "off",
            "multiline-ternary": "off"
        },
        ignores: ["dist/**", "node_modules/**"],
    }
);