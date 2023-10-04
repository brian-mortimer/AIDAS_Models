import cv2
from ultralytics import YOLO
import math

print("Loading model...")
model = YOLO("./ai_driver_assistant_cv/Models/Object_detection_model_v1/weights/best.pt")
# model = YOLO("yolov8n.pt")

if model == None:
    print("Failed to load model.")

print("Model loaded succesfully.")


# object classes for base YOLO model
# classNames = ["person", "bicycle", "car", "motorbike", "aeroplane", "bus", "train", "truck", "boat",
#               "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat",
#               "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella",
#               "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat",
#               "baseball glove", "skateboard", "surfboaqrd", "tennis racket", "bottle", "wine glass", "cup",
#               "fork", "knife", "spoon", "bowl", "banana", "apple", "sandwich", "orange", "broccoli",
#               "carrot", "hot dog", "pizza", "donut", "cake", "chair", "sofa", "pottedplant", "bed",
#               "diningtable", "toilet", "tvmonitor", "laptop", "mouse", "remote", "keyboard", "cell phone",
#               "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors",
#               "teddy bear", "hair drier", "toothbrush"
#               ]

# Model V1 classes
classNames = ["prohibitory", "danger", "mandatory", "other"]

# Define Video input
# vid = cv2.VideoCapture("./Testing/traffic-sign-to-test.mp4")
vid = cv2.VideoCapture("./Testing/footage_1.webm")
# vid = cv2.VideoCapture(0) # Web Camera

# Restrict video input to 640 x 480
vid.set(3,640)
vid.set(4,480)

# Get the frame rate of the video
fps = vid.get(cv2.CAP_PROP_FPS)

# Calculate the wait time between frames
wait_time = int(1000/fps)  # in milliseconds

# Set text characteristics
font = cv2.FONT_HERSHEY_SIMPLEX
fontScale = 1
color = (255, 0, 0)
thickness = 2


while True:
    ret , img = vid.read()
    
    if ret:
        results = model(img, stream=True, stream_buffer=True)
        
        for r in results:
            boxes = r.boxes
            for box in boxes:
                x1, y1, x2, y2 = box.xyxy[0]
                x1, y1, x2, y2 = int(x1), int(y1), int(x2), int(y2) # convert to int values
                
                # class name
                cls = int(box.cls[0])\

                # object details
                org = [x1, y1]

                confidence = math.ceil((box.conf[0]*100))/100

                cv2.putText(img, classNames[cls] + " " + str(confidence), org, font, fontScale, color, thickness)

                # put box in cam
                cv2.rectangle(img, (x1, y1), (x2, y2), (255, 0, 255), 3)
            
        cv2.imshow("Output", img)


    if cv2.waitKey(wait_time) & 0xFF == ord("q"):
        print("Terminated.")
        break

vid.release()

cv2.destroyAllWindows
