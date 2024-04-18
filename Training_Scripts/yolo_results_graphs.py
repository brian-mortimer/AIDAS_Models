import pandas as pd
import matplotlib.pyplot as plt

# Load the CSV file
file_path = './training_output/results.csv'  # Update this to your CSV file path
data = pd.read_csv(file_path)

# Correcting any potential issue with column names (removing leading/trailing spaces)
data.columns = data.columns.str.strip()

# Setting up the plots
fig, ax = plt.subplots(1, 3, figsize=(40, 8))

# Plotting training and validation losses
ax[0].plot(data['epoch'], data['train/box_loss'], label='Train Box Loss')
ax[0].plot(data['epoch'], data['val/box_loss'], label='Val Box Loss')
ax[0].plot(data['epoch'], data['train/cls_loss'], label='Train Class Loss')
ax[0].plot(data['epoch'], data['val/cls_loss'], label='Val Class Loss')
ax[0].plot(data['epoch'], data['train/dfl_loss'], label='Train DFL Loss')
ax[0].plot(data['epoch'], data['val/dfl_loss'], label='Val DFL Loss')
ax[0].set_xlabel('Epoch')
ax[0].set_ylabel('Loss')
ax[0].set_title('Training & Validation Losses Over Epochs')
ax[0].legend()

# Plotting precision and recall
ax[1].plot(data['epoch'], data['metrics/precision(B)'], label='Precision')
ax[1].plot(data['epoch'], data['metrics/recall(B)'], label='Recall')
ax[1].set_xlabel('Epoch')
ax[1].set_ylabel('Score')
ax[1].set_title('Precision & Recall Over Epochs')
ax[1].legend()

# Plotting mAP scores
ax[2].plot(data['epoch'], data['metrics/mAP50(B)'], label='mAP@50')
ax[2].plot(data['epoch'], data['metrics/mAP50-95(B)'], label='mAP@50-95')
ax[2].set_xlabel('Epoch')
ax[2].set_ylabel('mAP Score')
ax[2].set_title('mAP Scores Over Epochs')
ax[2].legend()

plt.tight_layout()
plt.show()
