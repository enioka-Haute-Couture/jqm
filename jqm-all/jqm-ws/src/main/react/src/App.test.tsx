import React from 'react';
import { render } from '@testing-library/react';
import App from './App';
import { describe } from 'node:test';
import { it } from 'vitest';

describe('renders app without crashing', () => {
    it('renders the app component', () => {
        render(<App />);
    });
});
