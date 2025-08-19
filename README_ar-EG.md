**العربية** | [English](README.md) | [Türkçe](README_tr-TR.md)

# SetBox
![Logo](https://github.com/YasserNull/setbox/blob/main/docs/images/logo.png)
<p align="center">
  <img src="https://img.shields.io/github/downloads/YasserNull/setbox/total?label=Downloads"/>
  <img src="https://img.shields.io/github/v/release/YasserNull/setbox?include_prereleases&label=Version"/>
  <img src="https://img.shields.io/badge/License-GPLv3-blue.svg"/>
</p>

## ما هو SetBox؟

**SetBox** هو تطبيق قوي يتيح لك تعديل إعدادات نظام أندرويد بسهولة عبر إضافات مطوَّرة من قبل المجتمع.

---

## لماذا تختار SetBox؟

يقدم SetBox مجموعة واسعة من الميزات، منها:

- التحكم في الإعدادات بسرعة
- تفعيل وتعطيل الميزات المخفية
- تحسينات مفيدة وبعض إصلاحات النظام

يعتمد SetBox على مساهمات المجتمع، مما يتيح لأي شخص إنشاء ومشاركة إضافاته الخاصة لتعزيز قدرات التطبيق.

---

## تفعيل SetBox

يتطلب تطبيق **SetBox** تفعيلًا لمرة واحدة باستخدام إحدى الطرق التالية:

- **صلاحيات الروت (Root Access)**: يتطلب جهازًا بصلاحيات الروت.
- **Shizuku**: استخدام تطبيق Shizuku للتفعيل بدون روت.
- **أوامر ADB**: منح الأذونات عبر ADB باستخدام الأمر التالي:

```bash
adb pm grant com.yn.setbox.plugin android.permission.WRITE_SECURE_SETTINGS
