/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        dark: '#0F172A',
        card: 'rgba(30, 41, 59, 0.7)',
        'card-hover': 'rgba(51, 65, 85, 0.8)',
        'accent-green': '#10B981',
        'accent-cyan': '#06B6D4',
      }
    },
  },
  plugins: [],
}

