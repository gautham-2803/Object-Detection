import os
import yaml
from tqdm import tqdm

def create_label_lookup(labels_list, labels_to_merge, final_label_name):
    """
        labels_list =  ['coffee-mug', 'chair', 'tea-mug', 'table']
        labels_to_merge = ['coffee-mug', 'tea-mug']
        final_label_name =  'mug'
        final_labels_list: ['mug', 'chair', 'table']
        idx_lookup: {1: 1, 3: 2, 0: 0, 2: 0}    
    """
    # creating a final labels list with the new label + labels that do not have to be merged 
    final_labels_list = [final_label_name] + [lbl for lbl in labels_list if lbl not in labels_to_merge]

    # creating a new set of indices. 
    new_idx = {
        lbl:idx for idx,lbl in enumerate(final_labels_list)
    }

    # capturing old indices
    old_idx = {
        lbl:idx for idx,lbl in enumerate(labels_list)
    }

    # creating a key value pair with new index and value
    idx_lookup = {}
    
    for lbl, idx in old_idx.items():
        if lbl not in labels_to_merge:
            idx_lookup[idx] =  new_idx[lbl]
        else:
            idx_lookup[idx] = new_idx[final_label_name]
    
    return final_labels_list, idx_lookup

# function to transform labels. 

def transform_labels(label_dir, idx_lookup):
    label_files = os.listdir(label_dir)

    # opening label files and reading their contents. 

    for lbl_file in tqdm(label_files):
        with open(os.path.join(label_dir, lbl_file), 'r+') as f:
            content_lines = f.readlines()
            f.seek(0)
            new_content_lines = []
            for c in content_lines:
                # since the first part is the class name we are replacing it with the new class index based on the lookup.
                 
                content = c.split()
                if len(content) == 0:

                    print(lbl_file,content)
                    continue
                content[0] = str(idx_lookup[int(content[0])])
                new_content = ' '.join(content) + "\n"
                new_content_lines.append(new_content)  
            new_content_lines[-1] = new_content_lines[-1].split("\n")[0]
            f.writelines(new_content_lines)

# function to load the yaml file

def read_data_yml(data_yml_path):
    content = {}
    with open(data_yml_path, "r") as f:
        content = yaml.safe_load(f)
    return content

# function to modify the yaml file

def modify_data_yml(data_yml_dict, final_labels_list):
    data_yml_dict['names'] = final_labels_list
    data_yml_dict['nc'] = len(final_labels_list)
    return data_yml_dict

# function to write back the yaml file
def write_data_yml(data_yml_dict, data_yml_path):
    with open(data_yml_path, "w") as f:
        yaml.dump(data_yml_dict, f, default_flow_style=False)

if __name__ == '__main__':
    labels_to_merge = input('Enter the labels to merge separated by commas: ').split(',')
    train_label_dir = input('Enter path to training labels directory: ')
    val_label_dir = input('Enter path to validation labels directory: ')
    test_label_dir = input('Enter path to test labels directory: ')
    final_label_name = input('Enter the final label name: ')
    data_yml_path = input('Enter path to data.yml: ')

    # Read data.yml and load into a dict
    data_yml_dict = read_data_yml(data_yml_path)

    # Create the final list of transformed labels and old_idx -> new_idx lookup
    final_labels_list, idx_lookup = create_label_lookup(
        data_yml_dict['names'], labels_to_merge, final_label_name
    )

    # Modify the old labels with the new label files
    transform_labels(train_label_dir, idx_lookup)
    
    if val_label_dir != '':
        transform_labels(val_label_dir, idx_lookup)
    
    if test_label_dir != '':
        transform_labels(test_label_dir, idx_lookup)

    # Modify and write the new data.yml contents with the merged label names
    data_yml_dict = modify_data_yml(data_yml_dict, final_labels_list)
    write_data_yml(data_yml_dict, data_yml_path)

