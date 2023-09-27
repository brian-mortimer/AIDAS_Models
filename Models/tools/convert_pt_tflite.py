from ultralytics import YOLO

model = YOLO("./Object_detection_model_v1/weights/best.pt")

model.export(format="tflite")

