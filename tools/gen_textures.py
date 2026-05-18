#!/usr/bin/env python3
"""Generates v0.1 placeholder textures for wireless redstone blocks.
Output: shared-resources/assets/wirelessredstone/textures/block/*.png

Run from the repo root:
    python tools/gen_textures.py
"""
from PIL import Image
import os

# Shared chassis palette
CHASSIS = (0x1F, 0x22, 0x28, 255)
PANEL   = (0x3A, 0x3F, 0x49, 255)
BEZEL   = (0x5A, 0x61, 0x72, 255)
LCD_BG  = (0x08, 0x09, 0x0C, 255)

# Transmitter LED palette
LCD_RED_MAIN   = (0x7B, 0x22, 0x22, 255)
LCD_GREEN_MAIN = (0x4F, 0xE8, 0x3E, 255)
LCD_GREEN_HALO = (0x2A, 0x8A, 0x20, 255)

# Receiver WiFi palette
WIFI_GRAY_ARC  = (0x5C, 0x65, 0x71, 255)
WIFI_GRAY_DOT  = (0x6C, 0x75, 0x81, 255)
WIFI_CYAN_ARC  = (0x5D, 0xDF, 0xFC, 255)
WIFI_CYAN_DOT  = (0x7D, 0xEF, 0xFF, 255)


def fill_rect(px, x1, y1, x2, y2, color):
    """Fill inclusive rectangle from (x1, y1) to (x2, y2)."""
    for y in range(y1, y2 + 1):
        for x in range(x1, x2 + 1):
            px[x, y] = color


def make_lcd(led_main, led_halo=None):
    img = Image.new('RGBA', (16, 16), CHASSIS)
    px = img.load()
    fill_rect(px, 1, 1, 14, 14, PANEL)      # panel rim
    fill_rect(px, 2, 2, 13, 13, BEZEL)      # LCD bezel
    fill_rect(px, 3, 3, 12, 12, LCD_BG)     # LCD recessed black
    if led_halo:
        fill_rect(px, 5, 5, 10, 10, led_halo)  # glow halo (on state only)
    fill_rect(px, 6, 6, 9, 9, led_main)     # LED 4x4 dot
    return img


def make_wifi(arc, dot):
    img = Image.new('RGBA', (16, 16), CHASSIS)
    px = img.load()
    fill_rect(px, 1, 1, 14, 14, PANEL)
    # center dot (2x2)
    fill_rect(px, 7, 11, 8, 12, dot)
    # inner arc
    fill_rect(px, 6, 9, 9, 9, arc)
    px[5, 10] = arc
    px[10, 10] = arc
    # outer arc
    fill_rect(px, 5, 6, 10, 6, arc)
    px[4, 7] = arc
    px[11, 7] = arc
    px[3, 8] = arc
    px[12, 8] = arc
    return img


def make_edge():
    img = Image.new('RGBA', (16, 16), CHASSIS)
    px = img.load()
    fill_rect(px, 1, 1, 14, 14, PANEL)
    return img


def make_back():
    img = Image.new('RGBA', (16, 16), CHASSIS)
    px = img.load()
    fill_rect(px, 1, 1, 14, 14, PANEL)
    return img


OUT_DIR = "shared-resources/assets/wirelessredstone/textures/block"
os.makedirs(OUT_DIR, exist_ok=True)

make_lcd(LCD_RED_MAIN).save(f"{OUT_DIR}/transmitter_lcd.png")
make_lcd(LCD_GREEN_MAIN, LCD_GREEN_HALO).save(f"{OUT_DIR}/transmitter_lcd_on.png")
make_wifi(WIFI_GRAY_ARC, WIFI_GRAY_DOT).save(f"{OUT_DIR}/receiver_wifi.png")
make_wifi(WIFI_CYAN_ARC, WIFI_CYAN_DOT).save(f"{OUT_DIR}/receiver_wifi_on.png")
make_edge().save(f"{OUT_DIR}/panel_edge.png")
make_back().save(f"{OUT_DIR}/panel_back.png")

print(f"Generated 6 textures in {OUT_DIR}")
