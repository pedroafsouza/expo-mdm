module.exports = {
  preset: 'jest-expo',
  testEnvironment: 'node',
  passWithNoTests: true,
  transform: {
    '^.+\\.tsx?$': ['babel-jest', { configFile: './node_modules/expo-module-scripts/babel.config.base.js' }],
  },
  moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx'],
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/**/__tests__/**',
  ],
};
