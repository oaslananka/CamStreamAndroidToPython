from ultralytics import YOLO
import cv2
import math
import torch

# Check GPU availability
device = 'cuda' if torch.cuda.is_available() else 'cpu'
print(f"Using device: {device}")

# RTSP URL
rtsp_url = "rtsp://<your_phone_ip>:1935"

# VideoCapture object
cap = cv2.VideoCapture(rtsp_url)
if not cap.isOpened():
    print("Cannot open RTSP stream")
    exit()

# Load the YOLO model and move to GPU if available
model = YOLO("yolo-Weights/yolov8n.pt").to(device)

# Object classes
class_names = [
    "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat", "traffic light",
    "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse", "sheep", "cow",
    "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee",
    "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard",
    "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
    "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "couch",
    "potted plant", "bed", "dining table", "toilet", "TV", "laptop", "mouse", "remote", "keyboard", "cell phone",
    "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear",
    "hair drier", "toothbrush", "sunglasses", "tape", "perfume bottle", "paper towel", "headphone case",
    "lamp", "trash can", "fan", "vacuum cleaner", "washing machine", "dryer", "iron", "mop", "broom",
    "desk", "window", "door", "mirror", "picture frame", "rug", "blanket", "pillow", "closet", "drawer",
    "wardrobe", "shelf", "cabinet", "table", "stool", "bench", "basket"
]

# Function to draw bounding boxes and labels on the image


def draw_boxes(img, boxes, class_names):
    for box in boxes:
        # Bounding box coordinates
        x1, y1, x2, y2 = box.xyxy[0]
        x1, y1, x2, y2 = int(x1), int(y1), int(x2), int(y2)

        # Draw bounding box
        cv2.rectangle(img, (x1, y1), (x2, y2), (255, 0, 255), 3)

        # Confidence score
        confidence = math.ceil((box.conf[0] * 100)) / 100
        print(f"Confidence: {confidence}")

        # Class name
        cls = int(box.cls[0])
        if cls < len(class_names):
            class_name = class_names[cls]
            print(f"Class name: {class_name}")

            # Put class name and confidence on the image
            label = f"{class_name} {confidence}"
            org = (x1, y1 - 10)
            font = cv2.FONT_HERSHEY_SIMPLEX
            font_scale = 1
            color = (255, 0, 0)
            thickness = 2

            cv2.putText(img, label, org, font, font_scale, color, thickness)


while True:
    success, img = cap.read()
    if not success:
        print("Failed to grab frame")
        break

    # Resize and convert to RGB
    img_resized = cv2.resize(img, (640, 640))
    img_rgb = cv2.cvtColor(img_resized, cv2.COLOR_BGR2RGB)

    # Convert image to tensor and normalize
    img_tensor = torch.from_numpy(img_rgb).to(device).float() / 255.0
    img_tensor = img_tensor.permute(2, 0, 1).unsqueeze(0)  # Convert to BCHW format

    # Model tahmini
    results = model(img_tensor, stream=True)

    for r in results:
        draw_boxes(img_resized, r.boxes, class_names)

    # Display the frame
    cv2.imshow('RTSP Stream', img_resized)
    if cv2.waitKey(1) == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()
