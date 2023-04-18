import cv2
import os
from tqdm import tqdm

def shuffle_data():
    images = sorted(os.listdir("traindata"))
    labels = sorted(os.listdir("trainlabels"))
    data_list = list()
    # creating a list of  tuple with img and labels
    for img, lbl in tqdm(zip(images, labels)):
        with open(os.path.join('trainlabels',lbl), "r") as f:
            lbl_val = f.read()
            data_list.append((cv2.imread((os.path.join('traindata',img))),lbl_val))
    import random; random.shuffle(data_list)
    counter = 1
    dir_final_images = "shuffled_images"
    dir_final_labels = "shuffled_labels"

    for item in tqdm(data_list):
        img_filename = os.path.join(dir_final_images,f'img_{counter}.jpg')
        label_filename = os.path.join(dir_final_labels,f'img_{counter}.txt')
        cv2.imwrite(img_filename,item[0])
        
        # write files

        with open(label_filename, "w") as f:
            f.write(item[1])
        counter += 1


if __name__ == '__main__':
    shuffle_data()
