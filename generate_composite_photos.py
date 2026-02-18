from PIL import Image, ImageOps
import os

def make_white_transparent(img):
    datas = img.getdata()
    newData = []
    # Threshold for white
    threshold = 240
    for item in datas:
        # Check if white (R,G,B > threshold)
        if item[0] > threshold and item[1] > threshold and item[2] > threshold: 
            newData.append((255, 255, 255, 0)) # Transparent
        else:
            newData.append(item)
    img.putdata(newData)
    return img

def create_composite(char_name, output_name):
    base_size = (1024, 1024)
    
    # 1. Load User Photo (Background)
    try:
        user_img = Image.open("app/src/main/res/drawable/user_child_photo.jpg").convert("RGBA")
        user_img = ImageOps.fit(user_img, base_size, method=Image.Resampling.LANCZOS)
    except Exception as e:
        print(f"Error loading user photo: {e}")
        return

    # 2. Load Frame
    try:
        frame_img = Image.open("app/src/main/res/drawable/ultraman_photo_frame.png").convert("RGBA")
        frame_img = frame_img.resize(base_size, Image.Resampling.LANCZOS)
        # Fix: Convert white center to transparent
        frame_img = make_white_transparent(frame_img)
    except Exception as e:
        print(f"Error loading frame: {e}")
        frame_img = Image.new("RGBA", base_size, (0,0,0,0)) # Transparent dummy

    # 3. Load Character
    try:
        char_path = f"app/src/main/res/drawable/char_sd_{char_name}.png"
        char_img = Image.open(char_path).convert("RGBA")
        
        # Resize Character (e.g. 500px high)
        char_ratio = char_img.width / char_img.height
        new_h = 500
        new_w = int(new_h * char_ratio)
        char_img = char_img.resize((new_w, new_h), Image.Resampling.LANCZOS)
        
        # Position: Bottom Right
        pos_x = base_size[0] - new_w - 50
        pos_y = base_size[1] - new_h - 50
    except Exception as e:
        print(f"Error loading character {char_name}: {e}")
        char_img = None

    # Composite: User -> Character -> Frame
    final_img = Image.new("RGBA", base_size)
    final_img.alpha_composite(user_img)
    if char_img:
        final_img.alpha_composite(char_img, (pos_x, pos_y))
    final_img.alpha_composite(frame_img)
    
    # Save
    output_path = f"app/src/main/res/drawable/{output_name}"
    final_img.convert("RGB").save(output_path, quality=90) # Save as JPG
    print(f"Saved {output_path}")

if __name__ == "__main__":
    create_composite("zero", "photo_pike_zero.jpg")
    create_composite("ace", "photo_pike_ace.jpg")
    create_composite("belial", "photo_pike_belial.jpg")
