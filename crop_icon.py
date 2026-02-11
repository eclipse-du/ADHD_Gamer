from PIL import Image, ImageDraw
import sys

def crop_circle(image_path, output_path, scale_factor=0.9):
    try:
        img = Image.open(image_path).convert("RGBA")
        width, height = img.size
        size = min(width, height)
        
        # Calculate crop size for "zoom" (smaller crop area = bigger subject)
        crop_size = int(size * scale_factor)
        
        # Center coordinates
        center_x = width // 2
        center_y = height // 2
        
        left = center_x - (crop_size // 2)
        top = center_y - (crop_size // 2)
        right = left + crop_size
        bottom = top + crop_size
        
        # Crop to square
        img = img.crop((left, top, right, bottom))
        
        # Resize back to original size (optional, or kept smaller)
        # Let's resize back to 512x512 standard or keep crop resolution?
        # Standard icon is 512 or 1024. Let's resize to 512x512 for consistency.
        target_size = 512
        img = img.resize((target_size, target_size), Image.Resampling.LANCZOS)
        
        # Create circular mask
        mask = Image.new('L', (target_size, target_size), 0)
        draw = ImageDraw.Draw(mask)
        draw.ellipse((0, 0, target_size, target_size), fill=255)
        
        # Apply mask
        output = Image.new('RGBA', (target_size, target_size))
        output.paste(img, (0, 0), mask=mask)
        
        output.save(output_path)
        print(f"Success: Saved to {output_path}")
        
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python crop_icon.py <input> <output>")
    else:
        # Scale factor 0.85 means we crop 85% of center (15% zoom in)
        crop_circle(sys.argv[1], sys.argv[2], scale_factor=0.85)
