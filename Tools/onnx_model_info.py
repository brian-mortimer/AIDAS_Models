import onnx

# Load the ONNX model
model_path = "C:/Users/Brian/Desktop/Projects/ai_driver_assistant_cv/Models/Traffic_Sign_model_v1/Road_Sign_Detection.onnx"
model = onnx.load(model_path)

def get_tensor_shape(tensor_info):
    tensor_shape = []
    if tensor_info.type.tensor_type.HasField("shape"):
        tensor_shape = [dim.dim_value for dim in tensor_info.type.tensor_type.shape.dim]
    return tensor_shape

input_shapes = {}
for input in model.graph.input:
    name = input.name
    shape = get_tensor_shape(input)
    input_shapes[name] = shape

output_shapes = {}
for output in model.graph.output:
    name = output.name
    shape = get_tensor_shape(output)
    output_shapes[name] = shape

print("Input Tensor Shapes:")
for input_name, input_shape in input_shapes.items():
    print(f"{input_name}: {input_shape}")

print("\nOutput Tensor Shapes:")
for output_name, output_shape in output_shapes.items():
    print(f"{output_name}: {output_shape}")