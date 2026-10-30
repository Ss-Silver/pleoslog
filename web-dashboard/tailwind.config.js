/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        hyundai: {
          blue: '#002c5f',
          sky: '#00aad2',
          sand: '#e4dcd3',
          dark: '#111827',
          gray: '#1f2937'
        }
      }
    },
  },
  plugins: [],
}
