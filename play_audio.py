import os
import random
from playsound import playsound

NAMES = ['air_conditioner', 'arm_chair', 'bed', 'cabinet', 'ceiling_fan', 'chair', 'drawer_near_bed', 'hanging_lights', 'lamp', 'makeup_chair', 'master_bed', 'men', 'photoframe', 'small_table', 'sofa', 'stool', 'table', 'wardrobe', 'women']

def play_audio_for_class(name):
    audio_folder_path = os.path.join(os.getcwd(),"audios")
    audio_path=os.path.join(audio_folder_path,f'{name}.mp3')
    playsound(audio_path)
    #print(audio_path)


if __name__ == "__main__":
    class_name = random.choice(NAMES)
    print(class_name)
    play_audio_for_class(class_name)



