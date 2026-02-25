using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Identity;

namespace resiliences_service.Enums
{
   public class RoleSeeder{
        private readonly RoleManager<IdentityRole<int>> _roleManager;

        public RoleSeeder(RoleManager<IdentityRole<int>> roleManager)
        {
            _roleManager = roleManager;
        }

        public async Task SeedRolesAsync()
        {
            string[] roles = { "ADMIN", "USER" };

            foreach (var role in roles)
            {
                if (!await _roleManager.RoleExistsAsync(role))
                {
                    await _roleManager.CreateAsync(new IdentityRole<int>(role));
                }
            }
        }
    }

}