import axios from 'axios';

const API_URL = 'http://localhost:8080/api/students'; // 这是你后端API的基础地址

const studentApiService = {
  getAllStudents() {
    return axios.get(API_URL);
  },

  getStudentById(id) {
    return axios.get(`${API_URL}/${id}`);
  },

  createStudent(studentData) {
    return axios.post(API_URL, studentData);
  },

  updateStudent(id, studentData) {
    return axios.put(`${API_URL}/${id}`, studentData);
  },

  deleteStudent(id) {
    return axios.delete(`${API_URL}/${id}`);
  }
};

export default studentApiService;