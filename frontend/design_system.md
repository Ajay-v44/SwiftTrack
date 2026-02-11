# SwiftTrack Design System

## Typography
- **Font Family**: Inter (Google Fonts)
- **Headings**:
  - H1: `text-4xl font-extrabold tracking-tight lg:text-5xl`
  - H2: `text-3xl font-semibold tracking-tight`
  - H3: `text-2xl font-semibold tracking-tight`
  - Body: `text-base leading-7`
  - Small: `text-sm font-medium leading-none`

## Colors (Tailwind Config)
```js
colors: {
  border: "hsl(var(--border))",
  input: "hsl(var(--input))",
  ring: "hsl(var(--ring))",
  background: "hsl(var(--background))",
  foreground: "hsl(var(--foreground))",
  primary: {
    DEFAULT: "hsl(222.2 47.4% 11.2%)", // Slate 900
    foreground: "hsl(210 40% 98%)",
  },
  secondary: {
    DEFAULT: "hsl(210 40% 96.1%)", // Slate 100
    foreground: "hsl(222.2 47.4% 11.2%)",
  },
  destructive: {
    DEFAULT: "hsl(0 84.2% 60.2%)", // Red 500
    foreground: "hsl(210 40% 98%)",
  },
  muted: {
    DEFAULT: "hsl(210 40% 96.1%)",
    foreground: "hsl(215.4 16.3% 46.9%)",
  },
  accent: {
    DEFAULT: "hsl(173 80% 40%)", // Teal 500
    foreground: "hsl(210 40% 98%)",
  },
  popover: {
    DEFAULT: "hsl(0 0% 100%)",
    foreground: "hsl(222.2 47.4% 11.2%)",
  },
  card: {
    DEFAULT: "hsl(0 0% 100%)",
    foreground: "hsl(222.2 47.4% 11.2%)",
  },
}
```

## Layouts
- **Container**: `container mx-auto px-4 md:px-6`
- **Section**: `py-12 md:py-24 lg:py-32`
- **Hero**: Full viewport height `min-h-[calc(100vh-4rem)]` with centered content.

## Components
- **Buttons**:
  - Primary: `bg-primary text-primary-foreground hover:bg-primary/90`
  - Secondary: `bg-secondary text-secondary-foreground hover:bg-secondary/80`
  - Outline: `border border-input hover:bg-accent hover:text-accent-foreground`
- **Inputs**: Rounded-md border input.
- **Cards**: Rounded-lg border bg-card text-card-foreground shadow-sm.

## Shadcn Integration
Use generic components from `shadcn/ui` and customize `tailwind.config.js` with above colors.
