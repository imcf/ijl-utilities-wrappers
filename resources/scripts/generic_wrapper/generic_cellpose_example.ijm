env_path = "D:\\conda_envs\\cellposeGPU204_113-821"
flags = "python -Xutf8 -m cellpose --pretrained_model cyto2 --chan 1 --diameter 15 --flow_threshold 0.4 --cellprob_threshold 0.0 --anisotropy 8.0 --verbose --save_tif --no_npy --use_gpu"
suffix = "_cp_masks"

run("Blobs (25K)");
run("Generic...", "env_path=["+env_path"+] flags=["+flags"+] suffix=["+suffix"+]");
selectWindow("blobs-wrapper");
