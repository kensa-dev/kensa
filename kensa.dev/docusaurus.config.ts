import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  title: 'Kensa',
  tagline: 'BDD Testing for Kotlin & Java',
  favicon: 'img/Logo.svg',

  // Future flags, see https://docusaurus.io/docs/api/docusaurus-config#future
  future: {
    v4: true, // Improve compatibility with the upcoming Docusaurus v4
  },

  url: 'https://kensa.dev',
  baseUrl: '/',

  // GitHub pages deployment config.
  organizationName: 'kensa-dev',
  projectName: 'kensa',

  trailingSlash: false,

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  themes: ['@saucelabs/theme-github-codeblock'],

  clientModules: [require.resolve('./src/clientModules/githubStars.ts')],

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.ts',
        },
        blog: {
          showReadingTime: true,
          blogSidebarTitle: 'All posts',
          blogSidebarCount: 'ALL',
        },
        sitemap: {
          changefreq: 'weekly',
          priority: 0.5,
        },
        gtag: {
          trackingID: 'G-XBGTKEJ8KH',
          anonymizeIP: true,
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    // Replace with your project's social card
    image: 'img/report-example.png',
    navbar: {
      title: 'Kensa',
      logo: {
        alt: 'Kensa Logo',
        src: 'img/Logo.svg',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'tutorialSidebar',
          position: 'left',
          label: 'Docs',
        },
        {
          type: 'doc',
          docId: 'quickstart/kotlin-quickstart',
          position: 'left',
          label: 'Quickstart',
        },
        {
          type: 'doc',
          docId: 'api/overview',
          position: 'left',
          label: 'API',
        },
        {
          to: '/blog',
          label: 'Blog',
          position: 'left',
        },
        {
          type: 'doc',
          docId: 'roadmap',
          position: 'left',
          label: 'Roadmap',
        },
        {
          href: 'https://github.com/kensa-dev/kensa',
          label: '⭐ Star',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      logo: {
        alt: 'Kensa',
        src: 'img/Logo.svg',
        width: 36,
        height: 36,
      },
      links: [
        {
          title: 'Learn',
          items: [
            { label: 'Quickstart', to: '/docs/quickstart/kotlin-quickstart' },
            { label: 'Documentation', to: '/docs/intro' },
            { label: 'API Reference', to: '/docs/api/overview' },
          ],
        },
        {
          title: 'Project',
          items: [
            { label: 'GitHub', href: 'https://github.com/kensa-dev/kensa' },
            { label: 'Discussions', href: 'https://github.com/kensa-dev/kensa/discussions' },
            { label: 'Releases', href: 'https://github.com/kensa-dev/kensa/releases' },
            { label: 'Issues', href: 'https://github.com/kensa-dev/kensa/issues' },
            { label: 'Maven Central', href: 'https://central.sonatype.com/search?q=dev.kensa' },
            { label: 'Privacy', to: '/privacy' },
          ],
        },
      ],
      copyright: `© ${new Date().getFullYear()} Kensa — BDD Testing for Kotlin &amp; Java`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
        additionalLanguages: ['java'],
      },
  } satisfies Preset.ThemeConfig,
};

export default config;
