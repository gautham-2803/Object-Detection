import os

def verify():
    images = sorted(os.listdir('traindata'))
    labels = sorted(os.listdir('trainlabels'))
    images = [img.split('.jpg')[0] for img in images]
    labels = [label.split('.txt')[0] for label in labels]
    verified_status = all([i==j for i,j in zip(images,labels)])
    print("All images and label names are the same: ", verified_status)

