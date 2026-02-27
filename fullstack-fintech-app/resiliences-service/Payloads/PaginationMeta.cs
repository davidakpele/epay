using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace resiliences_service.Payloads
{
    public class PaginationMeta
    {
        public long Page       { get; set; }
        public long PageSize   { get; set; }
        public long TotalCount { get; set; }
        public long TotalPages { get; set; }
    }
}