from PIL import Image, ImageDraw, ImageFont
import os

def create_placeholder(filename, text, color, size=(512, 512)):
    img = Image.new('RGBA', size, color)
    d = ImageDraw.Draw(img)
    # Try to load a font, fallback to default
    try:
        font = ImageFont.truetype("arial.ttf", 40)
    except IOError:
        font = ImageFont.load_default()
    
    # Draw text in center
    bbox = d.textbbox((0, 0), text, font=font)
    text_w = bbox[2] - bbox[0]
    text_h = bbox[3] - bbox[1]
    d.text(((size[0]-text_w)/2, (size[1]-text_h)/2), text, fill=(255, 255, 255), font=font)
    
    path = f"app/src/main/res/drawable/{filename}"
    img.save(path)
    print(f"Created {path}")

# Ensure directory exists
os.makedirs("app/src/main/res/drawable", exist_ok=True)

# Generate Assets
create_placeholder("ultraman_bg_graveyard.png", "Monster Graveyard\n(Placeholder)", (50, 0, 50), (1080, 1920))
create_placeholder("char_sd_zero.png", "Ultraman Zero\n(Placeholder)", (0, 0, 255))
create_placeholder("char_sd_belial.png", "Ultraman Belial\n(Placeholder)", (0, 0, 0))
create_placeholder("char_sd_ace.png", "Ultraman Ace\n(Placeholder)", (255, 100, 0))
create_placeholder("effect_beam_zero.png", "Zero Beam", (0, 255, 255), (200, 50))
create_placeholder("effect_beam_belial.png", "Belial Beam", (255, 0, 0), (200, 50))
create_placeholder("icon_ultraman_toy.png", "Ultraman Game", (200, 200, 200))
