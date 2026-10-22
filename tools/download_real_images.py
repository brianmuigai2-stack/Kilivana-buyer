"""Download product photographs from Wikimedia Commons and prepare app assets.

All sources are public domain or CC0. The source list and credits live in
docs/IMAGE_CREDITS.md. Requires Pillow.
"""

from __future__ import annotations

from pathlib import Path
from urllib.parse import quote
from urllib.request import Request, urlopen
import time

from PIL import Image, ImageOps


ROOT = Path(__file__).resolve().parents[1] / "app/src/main/res/drawable-nodpi"
COMMONS = "https://commons.wikimedia.org/wiki/Special:FilePath/"

PRODUCT_PHOTOS = {
    "produce_tomatoes": "Tomatoes.jpg",
    "produce_sukuma_wiki": "East African Vegetable (Sukuma wiki).jpg",
    "produce_spinach": "Fresh vegetables (1).jpg",
    "produce_cabbage": "Cabbage close up (49199572378).jpg",
    "produce_carrots": "Raw Carrots in Bowl.jpg",
    "produce_onions": "Sliced Onions.jpg",
    "produce_bananas": "Banana.png",
    "produce_avocados": "Abacate.jpg",
    "produce_mangoes": "Fresh mangoes and bananas.JPG",
    "produce_maize": "Maize - Corn.jpg",
    "produce_irish_potatoes": "Cut Potatoes.jpg",
    "produce_sweet_potatoes": "A sweet potato.jpg",
    "produce_green_beans": "Green beans - Massachusetts.jpg",
    "produce_beans": "Beans-g34a47f56f 1920.jpg",
    "produce_eggs": "A Tray Of Eggs.jpg",
    "produce_fresh_milk": "Milk 2.jpg",
}
BANNER_PHOTOS = {
    "banner_bulk_orders": ("Fresh vegetables (1).jpg", (1200, 500)),
    "banner_free_delivery": ("Fresh mangoes and bananas.JPG", (1200, 500)),
    "banner_seasonal": ("Maize - Corn.jpg", (1200, 500)),
}


def download(title: str, path: Path, size: tuple[int, int]) -> None:
    url = f"{COMMONS}{quote(title)}?width=1400"
    request = Request(url, headers={"User-Agent": "KilivanaBuyer/1.0"})
    with urlopen(request, timeout=60) as response:
        raw = response.read()
    from io import BytesIO
    image = Image.open(BytesIO(raw)).convert("RGB")
    image = ImageOps.fit(image, size, method=Image.Resampling.LANCZOS, centering=(0.5, 0.5))
    image.save(path, "JPEG", quality=86, optimize=True, progressive=True)
    time.sleep(1.2)


ROOT.mkdir(parents=True, exist_ok=True)
for resource_name, title in PRODUCT_PHOTOS.items():
    download(title, ROOT / f"{resource_name}.jpg", (800, 800))
for resource_name, (title, size) in BANNER_PHOTOS.items():
    download(title, ROOT / f"{resource_name}.jpg", size)

# Avoid Android duplicate resource errors when switching placeholder PNGs to photos.
for png in ROOT.glob("*.png"):
    png.unlink()
