from onnxruntime_extensions.tools import add_pre_post_processing_to_model as add_pre_post
import ultralytics
import shutil
from pathlib import Path


def convert_model_onnx(path, output_model_path):
    model = ultralytics.YOLO(path)
    onnx_file_name = model.export(format="onnx")
    shutil.move(onnx_file_name, output_model_path)


if __name__ == "__main__":
    model_path = Path("C:/Users/Brian/Desktop/Projects/ai_driver_assistant_cv/Models/Traffic_Sign_model_v1/Road_Sign_Detection.pt")
    onnx_model_path = Path("C:/Users/Brian/Desktop/Projects/ai_driver_assistant_cv/Models/Traffic_Sign_model_v1/Road_Sign_Detection.onnx")
    output_model_path = Path("C:/Users/Brian/Desktop/Projects/ai_driver_assistant_cv/Models/Traffic_Sign_model_v1/Road_Sign_Detection_pp.onnx")

    convert_model_onnx(model_path, onnx_model_path)

    add_pre_post.yolo_detection(onnx_model_path, output_model_path, "jpg", onnx_opset=18)

