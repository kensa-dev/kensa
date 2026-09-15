import React, {type ComponentProps, type ReactNode} from 'react';
import clsx from 'clsx';
import DefaultAdmonitionTypes from '@theme-original/Admonition/Types';
import AdmonitionLayout from '@theme/Admonition/Layout';
import type {Props} from '@theme/Admonition';

// A flask: stroke-based so it matches the site's line icons. The layout's stylesheet
// sets `fill` on admonition SVGs, so fill/stroke are pinned inline rather than as attributes.
function IconExperimental(props: ComponentProps<'svg'>): ReactNode {
  return (
    <svg
      viewBox="0 0 24 24"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      style={{fill: 'none', stroke: 'var(--ifm-alert-foreground-color)'}}
      {...props}>
      <path d="M9 3h6" />
      <path d="M10 3v6.4L4.6 18.2A2 2 0 0 0 6.3 21h11.4a2 2 0 0 0 1.7-2.8L14 9.4V3" />
      <path d="M7.5 15h9" />
    </svg>
  );
}

function AdmonitionTypeExperimental(props: Props): ReactNode {
  return (
    <AdmonitionLayout
      icon={<IconExperimental />}
      title="Experimental"
      {...props}
      className={clsx('alert alert--experimental', props.className)}>
      {props.children}
    </AdmonitionLayout>
  );
}

const AdmonitionTypes = {
  ...DefaultAdmonitionTypes,
  experimental: AdmonitionTypeExperimental,
};

export default AdmonitionTypes;
