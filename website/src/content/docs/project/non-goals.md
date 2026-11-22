---
title: What we are not building
description: Things the project has decided to leave out, and what to do instead.
---

A small core stays small by saying no. These are the things FireEscape will not do.

## Replacing the core through a plugin

The launcher core, including its focus engine, is not replaceable by a plugin. Everything around it is.

## Full applications inside the launcher

Plugins hand off to apps. The launcher does not host apps.

## Playing protected streaming content

That stays in the streaming apps. The launcher finds titles and opens the right app, but it never plays the content.

## Ad or sponsored suggestion insertion

Plugins that show ads or collect usage data are not eligible for the default index.

## Scheduled lineups and linear channels

Rows can appear on a schedule, but nothing here decides which title plays at what time.

## Settings sync across devices

Possible later. Not designed yet. Setup exports and imports as a file in the meantime.

## A security boundary for profiles

A launcher can hide and lock what its own screen shows. It cannot stop someone from opening Settings, installing a different launcher, or using ADB. For enforcement, use platform controls such as Google TV kids profiles. The Profiles class is for presentation and convenience on top of them.
