using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace resiliences_service.DTOs
{
    public class UserDTO
    {
        public long Id { get; set; }
        public string Email { get; set; } = default!;
        public string Username { get; set; } = default!;
        public bool Enabled { get; set; }
        public List<UserRecordDTO> Records { get; set; } = new List<UserRecordDTO>();
    }
}