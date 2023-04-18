import os 
import cv2
import imgaug.augmenters as ia
import glob
import uuid
import shutil
from tqdm import tqdm
from verify_preprocess import verify


def rename_files():
    images = sorted(os.listdir('traindata'))
    labels = sorted(os.listdir('trainlabels'))


    counter = 1

    img_prefix = os.path.join(os.getcwd(),'traindata')
    label_prefix = os.path.join(os.getcwd(),'trainlabels')

    for img, label in tqdm(zip(images, labels)):
        os.rename(os.path.join(img_prefix, img), os.path.join(img_prefix, f'img-{counter}.jpg'))
        os.rename(os.path.join(label_prefix, label), os.path.join(label_prefix, f'label-{counter}.txt'))
        counter += 1




def augment():
    #loading images from folder(multiple)
    images = sorted(os.listdir('traindata'))
    labels = sorted(os.listdir('trainlabels'))
    img_prefix = os.path.join(os.getcwd(),'traindata')
    label_prefix = os.path.join(os.getcwd(),'trainlabels')

    images_path = [os.path.join(img_prefix,img) for img in images]
    labels_path = [os.path.join(label_prefix,label) for label in labels]

    

    for img,label in tqdm(zip(images_path,labels_path)):
        img = cv2.imread(img)
        aug_images = []
        # add different transforms 
        aug = ia.Fliplr(0.5)
        # appends images to empty list
        aug_images.append(aug.augment_image(img))
        aug = ia.Flipud(0.5)
        aug_images.append(aug.augment_image(img))
        aug=ia.LinearContrast((0.6,0.6))
        aug_images.append(aug.augment_image(img))   
        aug = ia.GaussianBlur((0,3)) 
        aug_images.append(aug.augment_image(img))       
        
        # print(len(aug_images),"length")
        for aug_img in aug_images:
            # generate a random name for image and label files
            new_name = uuid.uuid4().hex
            # save the image file
            img_filename = os.path.join(img_prefix, f'{new_name}.jpg')
            cv2.imwrite(img_filename,aug_img)
            
            # save the label file

            label_filename = os.path.join(label_prefix, f'{new_name}.txt')
            shutil.copy(label,label_filename)


        
if __name__ == '__main__':
    augment()
    verify()
    rename_files()