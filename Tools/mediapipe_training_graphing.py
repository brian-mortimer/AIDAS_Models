import matplotlib.pyplot as plt
import re

file_path = './traffic_sign_v4_training_output.txt'

with open(file_path, 'r') as file:
    training_output = file.read()

training_output = training_output.replace('\n', "")

pattern = r"Epoch\s+(\d+).*?total_loss:\s+(\d+\.\d+).*?val_total_loss:\s+(\d+\.\d+)"

matches = re.findall(pattern, training_output)

epochs, total_losses, val_total_loss = zip(*[(int(m[0]), float(m[1]), float(m[2])) for m in matches])


# Plotting
plt.figure(figsize=(10, 6))

plt.plot(epochs, total_losses, label='Total Loss', marker='o', markersize=3)
plt.plot(epochs, val_total_loss, label='Val Total Loss', marker='s', markersize=3)

plt.xlabel('Epoch')
plt.ylabel('Loss')
plt.title('Training Loss Metrics Traffic Sign Detection Model V4')
plt.legend()
plt.grid(True)
plt.show()