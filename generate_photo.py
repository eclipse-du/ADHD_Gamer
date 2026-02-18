from PIL import Image, ImageDraw, ImageFont
import os

def create_photo(filename, text, color, size=(512, 512)):
    img = Image.new('RGB', size, color)
    d = ImageDraw.Draw(img)
    try:
        font = ImageFont.truetype("arial.ttf", 40)
    except IOError:
        font = ImageFont.load_default()
    
    bbox = d.textbbox((0, 0), text, font=font)
    text_w = bbox[2] - bbox[0]
    text_h = bbox[3] - bbox[1]
    d.text(((size[0]-text_w)/2, (size[1]-text_h)/2), text, fill=(0, 0, 0), font=font)
    
    path = f"app/src/main/res/drawable/{filename}"
    img.save(path)
    print(f"Created {path}")

os.makedirs("app/src/main/res/drawable", exist_ok=True)
create_photo("user_child_photo.jpg", "User Child Photo\n(Placeholder)", (255, 255, 200))
