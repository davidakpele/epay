"use client";

interface Props {
  onClose: () => void;
}

const SuccessModal = ({ onClose }: Props) => {
  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div className="bg-white rounded-2xl p-8 text-center max-w-sm w-full">
        <h2 className="text-xl font-semibold text-green-600">
          Bank Added Successfully
        </h2>

        <p className="mt-3 text-sm">
          Your bank account has been added successfully.
        </p>

        <button
          onClick={onClose}
          className="mt-6 w-full bg-green-600 text-white py-2 rounded-lg"
        >
          Done
        </button>
      </div>
    </div>
  );
};

export default SuccessModal;
